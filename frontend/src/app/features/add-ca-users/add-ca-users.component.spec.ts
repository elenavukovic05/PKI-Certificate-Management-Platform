import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCaUsersComponent } from './add-ca-users.component';

describe('AddCaUsersComponent', () => {
  let component: AddCaUsersComponent;
  let fixture: ComponentFixture<AddCaUsersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddCaUsersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddCaUsersComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
