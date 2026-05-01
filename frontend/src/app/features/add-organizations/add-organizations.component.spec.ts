import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddOrganizationsComponent } from './add-organizations.component';

describe('AddOrganizationsComponent', () => {
  let component: AddOrganizationsComponent;
  let fixture: ComponentFixture<AddOrganizationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddOrganizationsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddOrganizationsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
